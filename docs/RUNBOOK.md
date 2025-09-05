# Runbook

## Start locally
```bash
./gradlew bootRun
```
- gRPC on port 9090
- HTTP (actuator + H2 console) on port 8080

## Profiles
- **dev**: H2 in-memory, ddl-auto=update, H2 console enabled
- **test**: H2 in-memory, ddl-auto=create-drop, server/grpc ports=0
- **prod**: use PostgreSQL + Flyway

## Ports
- gRPC: 9090 (set `grpc.server.port`)
- HTTP: 8080 (set `server.port`)

## Health / Management
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/env`

## Common issues
- **Port in use**: set `server.port=0`, `grpc.server.port=0` in tests
- **grpcurl JSON errors in PowerShell**: use `--%` or pipe with `Get-Content -Raw`
