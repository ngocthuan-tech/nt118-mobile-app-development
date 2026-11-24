package com.aicoach.pose;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Camera + ML Kit Pose Detection, đóng gói để màn nào cần cũng dùng được (Kiểm tra sẵn sàng, Tập).
 *
 *  CameraX Preview ──▶ PreviewView (người dùng thấy mình)
 *  CameraX ImageAnalysis ──▶ ML Kit (luồng nền) ──▶ PoseFrame ──▶ Listener (main thread)
 *
 * Quyền riêng tư: khung hình chỉ nằm trong RAM, phân tích xong là đóng (imageProxy.close()),
 * không lưu, không gửi lên server.
 */
public final class PoseCamera {

    public interface Listener {
        /** Gọi trên main thread cho MỌI khung hình (kể cả khi không thấy người). */
        void onPoseFrame(@NonNull PoseFrame frame);
    }

    private static final String TAG = "PoseCamera";
    /** Ảnh phân tích nhỏ (640×480) là đủ cho 33 khớp, nhanh hơn nhiều so với Full HD. */
    private static final Size ANALYSIS_SIZE = new Size(640, 480);

    private final Context appContext;
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private final PoseDetector detector;

    private ProcessCameraProvider provider;
    private LifecycleOwner owner;
    private PreviewView previewView;
    private Listener listener;
    private boolean frontCamera = true;
    private volatile boolean closed;

    // Đo hiệu năng để ghi vào báo cáo (xem Logcat, lọc "PoseCamera")
    private long statWindowStart;
    private int statFrames;
    private long statLatencySum;

    public PoseCamera(@NonNull Context context) {
        appContext = context.getApplicationContext();
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        detector = PoseDetection.getClient(options);
    }

    /** Bật camera và bắt đầu phân tích. Camera tự tắt theo vòng đời {@code owner}. */
    public void start(@NonNull LifecycleOwner owner, @NonNull PreviewView previewView, @NonNull Listener listener) {
        this.owner = owner;
        this.previewView = previewView;
        this.listener = listener;

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(appContext);
        future.addListener(() -> {
            if (closed) return;
            try {
                provider = future.get();
                bind();
            } catch (Exception e) {
                Log.e(TAG, "Không mở được camera", e);
            }
        }, ContextCompat.getMainExecutor(appContext));
    }

    /** Đổi camera trước ↔ sau. */
    public void switchCamera() {
        frontCamera = !frontCamera;
        if (provider != null && !closed) bind();
    }

    public boolean isFrontCamera() {
        return frontCamera;
    }

    /** Gọi trong onDestroyView: tắt camera, giải phóng ML Kit và luồng nền. */
    public void close() {
        closed = true;
        if (provider != null) provider.unbindAll();
        detector.close();
        analysisExecutor.shutdown();
        owner = null;
        previewView = null;
        listener = null;
    }

    private void bind() {
        if (owner == null || previewView == null) return;

        // Preview và ảnh phân tích cùng tỉ lệ 4:3 → toạ độ khung xương khớp đúng với hình người dùng thấy
        ResolutionSelector previewSelector = new ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build();
        Preview preview = new Preview.Builder()
                .setResolutionSelector(previewSelector)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ResolutionSelector analysisSelector = new ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .setResolutionStrategy(new ResolutionStrategy(ANALYSIS_SIZE,
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                .build();
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setResolutionSelector(analysisSelector)
                // Máy chậm thì bỏ khung cũ, chỉ xử lý khung mới nhất → không bị trễ dồn
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(analysisExecutor, this::analyze);

        CameraSelector selector = frontCamera
                ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            if (!provider.hasCamera(selector)) {
                frontCamera = !frontCamera;
                selector = frontCamera ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            }
            provider.unbindAll();
            provider.bindToLifecycle(owner, selector, preview, analysis);
        } catch (Exception e) {
            Log.e(TAG, "Không gắn được camera", e);
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyze(@NonNull ImageProxy proxy) {
        Image media = proxy.getImage();
        if (media == null || closed) {
            proxy.close();
            return;
        }
        int rotation = proxy.getImageInfo().getRotationDegrees();
        boolean swap = rotation == 90 || rotation == 270;
        final int width = swap ? proxy.getHeight() : proxy.getWidth();
        final int height = swap ? proxy.getWidth() : proxy.getHeight();
        final float brightness = averageLuma(proxy);
        final long frameMs = proxy.getImageInfo().getTimestamp() / 1_000_000L;
        final long startNs = System.nanoTime();

        InputImage image = InputImage.fromMediaImage(media, rotation);
        detector.process(image)
                .addOnSuccessListener(pose -> {
                    long latencyMs = (System.nanoTime() - startNs) / 1_000_000L;
                    logStats(latencyMs);
                    Listener l = listener;
                    if (!closed && l != null) {
                        l.onPoseFrame(toFrame(pose, width, height, brightness, frameMs, latencyMs));
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "ML Kit lỗi khung hình", e))
                .addOnCompleteListener(task -> proxy.close());
    }

    private static PoseFrame toFrame(Pose pose, int width, int height, float brightness, long timeMs, long latencyMs) {
        PoseFrame.Builder b = new PoseFrame.Builder(width, height)
                .brightness(brightness)
                .time(timeMs)
                .latency(latencyMs);
        for (PoseLandmark lm : pose.getAllPoseLandmarks()) {
            b.point(lm.getLandmarkType(), lm.getPosition().x, lm.getPosition().y, lm.getInFrameLikelihood());
        }
        return b.build();
    }

    /** Độ sáng trung bình từ kênh Y (lấy mẫu thưa cho nhanh). */
    private static float averageLuma(ImageProxy proxy) {
        ImageProxy.PlaneProxy plane = proxy.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        int w = proxy.getWidth();
        int h = proxy.getHeight();
        long sum = 0;
        int n = 0;
        for (int y = 0; y < h; y += 16) {
            for (int x = 0; x < w; x += 16) {
                int index = y * rowStride + x * pixelStride;
                if (index < buffer.limit()) {
                    sum += buffer.get(index) & 0xFF;
                    n++;
                }
            }
        }
        return n == 0 ? 128f : (float) sum / n;
    }

    private void logStats(long latencyMs) {
        long now = System.currentTimeMillis();
        if (statWindowStart == 0) statWindowStart = now;
        statFrames++;
        statLatencySum += latencyMs;
        long window = now - statWindowStart;
        if (window >= 3000) {
            Log.i(TAG, String.format(java.util.Locale.US, "AI: %.1f khung/giây, trễ trung bình %d ms",
                    statFrames * 1000f / window, statLatencySum / statFrames));
            statWindowStart = now;
            statFrames = 0;
            statLatencySum = 0;
        }
    }
}
