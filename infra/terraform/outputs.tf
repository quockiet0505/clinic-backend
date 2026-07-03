output "vm_name" {
  description = "The name of the Virtual Machine"
  value       = google_compute_instance.clinic_vm.name
}

output "vm_ip" {
  description = "The static external IP of the Virtual Machine"
  value       = data.google_compute_address.clinic_static_ip.address
}
