output "instance_ip" {
  description = "Public IP of the EC2 instance (Elastic IP)"
  value       = aws_eip.app.public_ip
}

output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

output "security_group_id" {
  description = "Application security group ID"
  value       = aws_security_group.app.id
}
