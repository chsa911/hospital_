# Hospital Service (Spring Boot + gRPC)
## Highlights

- **Clean JPA modeling**
    - Kein `@Data` auf Entities (nur Getter/Setter)
    - `equals`/`hashCode` ausschließlich auf IDs
    - Bidirektionale Beziehungen mit Helper-Methoden (`addPatient`, `removePatient`)
- **Robuste Businesslogik**
    - Idempotente Registrierung (`registerPatient`)
    - Abgesicherte Fehlerbehandlung (`EntityNotFoundException` statt `Optional.get()`)
    - Hospital-Löschung entfernt **nicht** die Patienten
- **Tests auf allen Ebenen**
    - Slice Tests (`@DataJpaTest`) für Repositories & Services (schnell, kein Port-Bind)
    - Service Tests für Businessregeln: Billing, Cancellations, Quarter Summaries
    - Integrationstests optional
- **Produktionsreife Infrastruktur**
    - Logging konfiguriert (Hibernate SQL + Bind Parameter, eigenes Package auf DEBUG)
- **Dokumentation für Entwickler**
    - `README.md` mit Quickstart & grpcurl Beispielen
    - grpcurl -plaintext -d '{}' localhost:9090 hospital.HospitalService/ListHospitals

    - `ARCHITECTURE.md` (C4 Container Diagramm)
    - `DATA_MODEL.md` (ER Diagramm, Cascade-Regeln)
    - `API.md` (aus `hospital.proto` + grpcurl Beispiele)
    - `RUNBOOK.md` (Ports, Profile, Troubleshooting)
- **Tooling & Ops Awareness**
    - Actuator Endpoints (`/health`, `/metrics`, `/env`)
    - Reflection Service für grpcurl / Postman
    - Bash-Skripte zum automatischen Aufruf der gRPC-Endpunkte
- **Sofort erlebbar**
    - `data.sql` mit Beispiel-Hospitals, Patienten, Stays & Bills
    - `grpcurl` und PowerShell-freundliche Beispiele dokumentiert

**What it does**
- Manage hospitals & patients, register patients to hospitals
- Track stays; create/cancel stays; quarter summaries
- Bills = total stay days × 200; list bills & outstanding balance

**Quickstart**
```bash
./gradlew bootRun              # gRPC :9090, HTTP :8080 (H2 console)
# or choose ports:
./gradlew bootRun --args="--server.port=8081 --grpc.server.port=9090"


├─ README.md                # Quickstart & overview
├─ docs/
│  ├─ architecture.md       # Deep dive: modules, ports, tech stack
│  ├─ api/                  # Generated API docs from .proto
│  │  └─ hospital.html      # (protoc-gen-doc output)
│  ├─ erd.md                # Data model / ER diagrams
│  ├─ adr/                  # Architecture Decision Records (optional)
│  │  └─ 0001-use-grpc.md
├─ CONTRIBUTING.md          # How to develop, branch naming, PRs
├─ LICENSE                  # Project license (if open source)
└─ src/main/proto           # Your .proto files (source of truth)
