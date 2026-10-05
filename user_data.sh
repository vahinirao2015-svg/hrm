#!/bin/bash
# Ubuntu 22.04 bootstrap. Log: /var/log/user-data.log
exec > /var/log/user-data.log 2>&1
set -x
export DEBIAN_FRONTEND=noninteractive

step() { echo "=== $1 ($(date -u +%T)) ==="; }

step "Swap"
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

step "Base packages"
apt-get update -y
apt-get install -y openjdk-17-jdk maven git curl ca-certificates gnupg unzip

step "Docker"
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
usermod -aG docker ubuntu
docker compose version || echo "WARNING: compose plugin missing"

step "Jenkins"
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key -o /usr/share/keyrings/jenkins-keyring.asc
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" > /etc/apt/sources.list.d/jenkins.list
apt-get update -y
apt-get install -y jenkins
usermod -aG docker jenkins
systemctl enable --now jenkins
systemctl restart jenkins

step "Data volume (falls back to root disk if EBS is not attached)"
for i in $(seq 1 150); do [ -b /dev/xvdf ] && break; sleep 2; done
mkdir -p /data
if [ -b /dev/xvdf ]; then
  blkid /dev/xvdf || mkfs.ext4 /dev/xvdf
  mount /dev/xvdf /data \
    && echo "$(blkid -s UUID -o export /dev/xvdf | head -1) /data ext4 defaults,nofail 0 2" >> /etc/fstab
else
  echo "WARNING: /dev/xvdf not found, using root disk for /data"
fi
mkdir -p /data/postgres /data/sonarqube

step "SonarQube"
sysctl -w vm.max_map_count=262144 fs.file-max=65536
printf "vm.max_map_count=262144\nfs.file-max=65536\n" >> /etc/sysctl.conf
chown -R 1000:1000 /data/sonarqube
docker run -d --name sonarqube --restart unless-stopped -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  -v /data/sonarqube:/opt/sonarqube/data sonarqube:community

step "DONE"
