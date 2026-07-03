# Tham chiếu đến IP tĩnh đã có (Dùng data để không bị tạo mới)
data "google_compute_address" "clinic_static_ip" {
  name   = "clinic-static-ip"
  region = var.region
}
