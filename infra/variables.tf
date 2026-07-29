variable "project_name" {
  description = "Project name — used to prefix all cloud resources"
  type        = string
}

variable "region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "ssh_public_key" {
  description = "SSH public key material injected by the platform"
  type        = string
  sensitive   = true
}
