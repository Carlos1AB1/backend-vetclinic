# Script simple para subir Backend
$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"

cd C:\Users\ByArc\Desktop\backend-vetclinic-1
scp -i $keyPath -r -o "StrictHostKeyChecking=no" . ubuntu@${serverIP}:/home/ubuntu/backend-vetclinic-1

Write-Host "✅ Backend subido" -ForegroundColor Green

