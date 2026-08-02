# backbone

## Local observability

This service is wired for OpenTelemetry traces, metrics, and logs through an
OpenTelemetry Collector. The collector exports to a local Elastic Stack.

Expected local services:

- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

Build the application jar before starting the Compose profile:

```sh
./mvnw clean package
docker compose --profile prod up
```

The Compose setup downloads the OpenTelemetry Java Agent into a named Docker
volume and starts:

- `spring-backbone` on `http://localhost:8000`
- `backbone-otel-collector` with OTLP on `4317` and `4318`
- collector health check on `http://localhost:13133`

The collector sends telemetry to:

```sh
http://host.docker.internal:9200/_otlp
```

Override the Elastic OTLP endpoint when needed:

```sh
ELASTIC_OTLP_ENDPOINT=http://host.docker.internal:9200/_otlp docker compose --profile prod up
```

Generate telemetry with:

```sh
curl http://localhost:8000/home
```

Then check Kibana Observability for the `backbone` service, request traces,
JVM/runtime metrics, and application logs.
