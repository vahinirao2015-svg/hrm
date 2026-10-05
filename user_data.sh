#!/bin/bash
set -ex
exec > /var/log/user-data.log 2>&1
export DEBIAN_FRONTEND=noninteractive

# 2G swap (t2.medium has 4G RAM; Jenkins + SonarQube are heavy)
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# Persistent data volume
for i in $(seq 1 30); do [ -b /dev/xvdf ] && break; sleep 2; done
blkid /dev/xvdf || mkfs.ext4 /dev/xvdf
mkdir -p /data && mount /dev/xvdf /data
echo "$(blkid -s UUID -o export /dev/xvdf | head -1) /data ext4 defaults,nofail 0 2" >> /etc/fstab
mkdir -p /data/postgres /data/sonarqube

apt-get update -y
apt-get install -y openjdk-17-jdk maven git curl ca-certificates gnupg unzip

# Docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker ubuntu
apt-get install -y docker-compose-plugin

# Jenkins
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key -o /usr/share/keyrings/jenkins-keyring.asc
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" > /etc/apt/sources.list.d/jenkins.list
apt-get update -y && apt-get install -y jenkins
usermod -aG docker jenkins
systemctl enable --now jenkins
systemctl restart jenkins

# SonarQube (Docker, data on persistent volume)
sysctl -w vm.max_map_count=262144 fs.file-max=65536
echo -e "vm.max_map_count=262144\nfs.file-max=65536" >> /etc/sysctl.conf
chown -R 1000:1000 /data/sonarqube
docker run -d --name sonarqube --restart unless-stopped -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  -v /data/sonarqube:/opt/sonarqube/data sonarqube:lts-community
