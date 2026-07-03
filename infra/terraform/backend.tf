terraform {
  backend "gcs" {
    bucket = "clinicqa-storage"
    prefix = "terraform/state"
  }
}
