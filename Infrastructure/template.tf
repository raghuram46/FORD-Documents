# Containers are the unit of deployment in Cloud Run. We need a container
# registry to store our container images.
resource "google_container_registry" "registry" {
  location = "US"
}

#secrets from GCP
locals {
  otel_environment = module.otel-env-configuration.env
  app_configs      = [
    {
      name  = "COMMON_PROJECT_NUMBER"
      value = var.project_number
    },
    {
      name  = "COMMON_PROJECT_ID"
      value = var.project
    },
    {
      name  = "PSA_PROJECT_NUMBER"
      value = var.psa_project_number
    },
    {
      name  = "PSA_PROJECT_ID"
      value = var.psa_project_id
    }
  ]
  app_environment    = concat(local.otel_environment, local.app_configs)
  secret_environment = [
    {
      name           = "GBMS_DBURL"
      secret_name    = "gbms-gcp-mssql-dburl"
      secret_version = "latest"
    },
    {
      name           = "GBMS_DBUSER"
      secret_name    = "gbms-gcp-mssql-dbuser"
      secret_version = "latest"
    },
    {
      name           = "GBMS_DBPASSWORD"
      secret_name    = "gbms-gcp-mssql-dbpassword"
      secret_version = "latest"
    },
    {
      name           = "OTEL_EXPORTER_OTLP_HEADERS"
     secret_name    = "otel-collector-service-authorization"
      secret_version = "latest"
    },
    {
      name           = "CONCEP_DB_URL"
      secret_name    = "conceps-connection-url"
      secret_version = "latest"
    },
    {
      name           = "CONCEP_DB_USERNAME"
      secret_name    = "conceps-connection-username"
      secret_version = "latest"
    },
    {
      name           = "CONCEP_DB_PASSWORD"
      secret_name    = "conceps-connection-password"
      secret_version = "latest"
    },
    {
      name           = "GBMS_SMTPPASSWORD"
      secret_name    = "gbms-smtp-password"
      secret_version = "latest"
    }
  ]
}

#OTel Configuration module
module "otel-env-configuration" {
  source             = git@github.ford.com:Pro-Tech/operational-metrics-terraform.git//modules/otel-configuration?ref=v2.0.12
  gcp_project_id     = var.project
  service_name       = var.cloud_run_name
  service_version    = "1.0.0"
  trace_sample_rate  = "1.0"
  product_group_name = "Sales Solution"
  product_line_name  = "Incentives & Bids "
  product_name       = "Government Deals"
  eams_id            = "23367"
  environment        = lower(var.environment)
}

#Datadog dashboard module
module "datadog-dashboard" {
  source           = git@github.ford.com:Pro-Tech/operational-metrics-terraform.git//modules/dashboard-tfm?ref=v2.0.12
  gcp_project_id   = var.project
  service_name     = var.cloud_run_name
  environment      = lower(var.environment)
  dashboard_name   = var.dashboard_name
  apigee_base_path = var.apigee_base_path
}

#Datadog SLO APIGEE Module
module "datadog-slo-apigee-monitor" {
  source                          = git@github.ford.com:Pro-Tech/operational-metrics-terraform.git//modules/datadog-slo-apigee-monitors?ref=v2.0.12
  gcp_project_id                  = var.project
  service_name                    = var.cloud_run_name
  slo_name                        = var.slo_name
  slo_description                 = var.slo_description
  target_service_level_objective  = var.target_service_level_objective
  warning_service_level_objective = var.warning_service_level_objective
  primary_slo_timeframe           = var.primary_slo_timeframe
  environment                     = lower(var.environment)
  monitor_name                    = var.monitor_name
  priority                        = var.priority
  critical_error_budget           = var.critical_error_budget
  warning_error_budget            = var.warning_error_budget
  critical_burn_rate              = var.critical_burn_rate
  warning_burn_rate               = var.warning_burn_rate
  burn_rate_long_window           = var.burn_rate_long_window
  burn_rate_short_window          = var.burn_rate_short_window
  notify_no_data                  = var.notify_no_data
  renotify_interval               = var.renotify_interval
  timeout_h                       = var.timeout_h
  slack_channel_name              = var.slack_channel_name
  victorops_team_name             = var.victorops_team_name
  monitor_alert_window            = var.monitor_alert_window
  apigee_base_path                = var.apigee_base_path
  product_name                    = var.product_name
  product_group_name              = var.product_group_name
  product_line_name               = var.product_line_name
  eams_id                         = var.eams_id
}

#Otel secret manager member
resource "google_secret_manager_secret_iam_member" "member" {
  count     = length(local.secret_environment)
  secret_id = local.secret_environment[count.index].secret_name
  project   = var.project
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.service_accounts.email}" #add the cloud run service account
}

#Cloud Run Service Account
module "service_accounts" {
  source        = "github.com/terraform-google-modules/terraform-google-service-accounts.git?ref=v4.1.1"
  project_id    = var.project
  names         = ["gbms-priceprotection-service"]
  description   = "Service account for the gbms-priceprotection-service api service"
  display_name  = "Api service account"
  project_roles = []
}

module "cloud_run" {
  source                = git@github.ford.com:gcp/tfm-cloud-run.git
  gcp_project_id        = var.project
  service_name          = var.cloud_run_name
  service_image_url     = "us-central1-docker.pkg.dev/${var.project}/ford-container-images/${var.container_image}:${var.image_tag}"
  service_account_email = module.service_accounts.email
  service_invoker       = ["serviceAccount:sa-pipeline@${var.project}.iam.gserviceaccount.com"]
  # Service_invoker can be a list of service accounts that can invoke the cloud run service and/or a list of groups(team) and/or list of users i.e. service_invoker  = ["serviceAccount:sa-pipeline@<project-id>.iam.gserviceaccount.com", "group:<your-group>>@ford.com","user:<your-user-name>>@ford.com"] - change this according to your needs
  ingress_traffic_type  = "internal"
  service_vpc_connector = var.service_vpc_connector
  gcp_region            = var.region
  min_instance_count    = "2"
  memory_size           = "8000"
  container_concurrency = "1000"
  cpu_count             = "2"
  apigee_environment    = var.environment # Apigee environment to which you plan to publish this service as an API
  environment           = local.app_environment
  secret_environment    = local.secret_environment
  depends_on            = [
    # Lists resource that must be completed before the the cloud run service is created
    google_secret_manager_secret_iam_member.member
  ]
}

output "cloud_run_url" {
  description = "URL for invoking the cloud run service"
  value       = module.cloud_run.url
}

 