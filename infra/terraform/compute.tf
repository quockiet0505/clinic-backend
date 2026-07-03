# Tự động Import máy ảo hiện tại vào Terraform (Chỉ áp dụng cho Terraform 1.5.0 trở lên)
import {
  to = google_compute_instance.clinic_vm
  id = "projects/clinicqa-500817/zones/asia-southeast1-a/instances/clinic-vm"
}

# Cấu hình máy ảo
resource "google_compute_instance" "clinic_vm" {
  name         = "clinic-vm"
  machine_type = var.machine_type
  zone         = var.zone

  tags = ["http-server", "https-server"]

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2404-lts"
      size  = var.disk_size
      type  = "pd-balanced"
    }
  }

  network_interface {
    network = "default"

    access_config {
      # Sử dụng IP tĩnh đã khai báo ở trên thông qua data block
      nat_ip = data.google_compute_address.clinic_static_ip.address
      network_tier = "PREMIUM"
    }
  }

  service_account {
    email  = var.sa_email
    scopes = ["cloud-platform"]
  }

  shielded_instance_config {
    enable_secure_boot          = true
    enable_vtpm                 = true
    enable_integrity_monitoring = true
  }

  metadata = {
    enable-osconfig = "TRUE"
  }

  allow_stopping_for_update = true

  lifecycle {
    prevent_destroy = true
    ignore_changes  = [
      metadata["ssh-keys"],
      labels,
      boot_disk[0].initialize_params[0].image
    ]
  }
}
