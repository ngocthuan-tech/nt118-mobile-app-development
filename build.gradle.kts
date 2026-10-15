// Cấu hình cấp project — chỉ khai báo plugin, không thêm dependency ở đây.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.navigation.safeargs) apply false
}
