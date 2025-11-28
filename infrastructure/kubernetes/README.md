# Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the ServiceDesk Platform.

## Structure

```
kubernetes/
├── base/                    # Base manifests (shared across environments)
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── postgres.yaml
│   ├── redis.yaml
│   ├── ticket-service.yaml
│   ├── channel-service.yaml
│   ├── knowledge-service.yaml
│   ├── notification-service.yaml
│   ├── ai-service.yaml
│   ├── api-gateway.yaml
│   ├── ingress.yaml
│   └── kustomization.yaml
│
└── overlays/               # Environment-specific overlays
    ├── staging/
    │   └── kustomization.yaml
    └── production/
        └── kustomization.yaml
```

## Prerequisites

- Kubernetes 1.25+
- kubectl configured
- Kustomize (built into kubectl 1.14+)
- NGINX Ingress Controller
- cert-manager (for TLS)

## Deployment

### Staging

```bash
# Preview changes
kubectl kustomize infrastructure/kubernetes/overlays/staging

# Apply
kubectl apply -k infrastructure/kubernetes/overlays/staging
```

### Production

```bash
# Preview changes
kubectl kustomize infrastructure/kubernetes/overlays/production

# Apply
kubectl apply -k infrastructure/kubernetes/overlays/production
```

## Configuration

### Secrets

**Important**: Update the secrets before deploying to production!

```bash
# Create secrets from command line
kubectl create secret generic servicedesk-secrets \
  --namespace=servicedesk \
  --from-literal=DB_PASSWORD=your-secure-password \
  --from-literal=JWT_SECRET=your-256-bit-secret-key \
  --from-literal=RABBITMQ_PASSWORD=your-password \
  --dry-run=client -o yaml | kubectl apply -f -
```

Or use sealed-secrets/external-secrets-operator for production.

### Ingress

Update the host in `ingress.yaml` or overlay to match your domain:

```yaml
spec:
  rules:
    - host: your-domain.com
```

### Storage

The base configuration uses default storage class. For production, specify your storage class:

```yaml
volumeClaimTemplates:
  - spec:
      storageClassName: your-storage-class
```

## Scaling

Services are configured with HorizontalPodAutoscaler (HPA):

| Service | Min | Max | CPU Threshold |
|---------|-----|-----|---------------|
| ticket-service | 2 | 10 | 70% |
| api-gateway | 2 | 5 | 70% |
| Other services | 2 | N/A | N/A |

## Health Checks

All services expose health endpoints:

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

## Monitoring

Deploy Prometheus and Grafana for monitoring:

```bash
# Add Prometheus community repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts

# Install kube-prometheus-stack
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace
```

## Troubleshooting

### Check pod status
```bash
kubectl get pods -n servicedesk
```

### View logs
```bash
kubectl logs -f deployment/ticket-service -n servicedesk
```

### Describe pod
```bash
kubectl describe pod <pod-name> -n servicedesk
```

### Port forward for debugging
```bash
kubectl port-forward svc/api-gateway 8080:8080 -n servicedesk
```
