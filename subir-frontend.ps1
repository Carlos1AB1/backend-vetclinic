# Script simple para subir Frontend
$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"

cd C:\Users\ByArc\Desktop\frontend-vetclinio-1
scp -i $keyPath -r -o "StrictHostKeyChecking=no" . ubuntu@${serverIP}:/home/ubuntu/frontend-vetclinio-1

Write-Host "✅ Frontend subido" -ForegroundColor Green

