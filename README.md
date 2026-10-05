# Salohi IT HRMS

## 1. Infrastructure
cd terraform
terraform init
terraform apply -var key_name=<your-key> -var allowed_cidr=<your-ip>/32
Wait ~5 min, then: Jenkins :8080 (password: `sudo cat /var/lib/jenkins/secrets/initialAdminPassword`), SonarQube :9000 (admin/admin).

## 2. Run the app (on the EC2 box, in hrms/)
export DB_PASSWORD=<strong> ADMIN_PASSWORD=<strong>
docker compose up -d --build
DB files live on the EBS volume at /data/postgres, so they survive container and instance replacement.

## 3. API (HTTP Basic auth; first login admin / $ADMIN_PASSWORD)
GET/POST /api/users, PUT/DELETE /api/users/{id}      ADMIN only
GET /api/employees                                    ADMIN, USER
POST/PUT/DELETE /api/employees                        ADMIN only
