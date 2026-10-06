terraform {
  required_providers { aws = { source = "hashicorp/aws", version = "~> 5.0" } }
}
provider "aws" { region = var.region }

data "aws_vpc" "default" { default = true }

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

resource "aws_security_group" "devops" {
  name        = "salohi-devops-sg"
  description = "SSH, Jenkins, SonarQube, HRMS app"
  vpc_id      = data.aws_vpc.default.id
  dynamic "ingress" {
    for_each = { ssh = 22, jenkins = 8080, sonarqube = 9000, hrms = 8081 }
    content {
      description = ingress.key
      from_port   = ingress.value
      to_port     = ingress.value
      protocol    = "tcp"
      cidr_blocks = [var.allowed_cidr]
    }
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "devops" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "m7i-flex.large"
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.devops.id]
  user_data              = file("${path.module}/user_data.sh")
  user_data_replace_on_change = true
  root_block_device {
    volume_size = 50
    volume_type = "gp3"
  }
  tags = { Name = "salohi-devops" }
}

# Separate persistent EBS volume for database files (survives instance replacement)
resource "aws_ebs_volume" "data" {
  availability_zone = aws_instance.devops.availability_zone
  size              = var.data_volume_gb
  type              = "gp3"
  tags              = { Name = "salohi-hrms-data" }
}
resource "aws_volume_attachment" "data" {
  device_name = "/dev/xvdf"
  volume_id   = aws_ebs_volume.data.id
  instance_id = aws_instance.devops.id
}
