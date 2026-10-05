output "public_ip" { value = aws_instance.devops.public_ip }
output "jenkins_url" { value = "http://${aws_instance.devops.public_ip}:8080" }
output "sonar_url" { value = "http://${aws_instance.devops.public_ip}:9000" }
output "hrms_url" { value = "http://${aws_instance.devops.public_ip}:8081" }
