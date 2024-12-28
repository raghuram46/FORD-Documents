variable "project" {
  type    = string
  default = "ford-9f6dee5062e076ddeccf7278"
}

variable "region" {
  type    = string
  default = "us-east4"
}

variable "environment" {
  type    = string
  default = "DEV"
}

variable "container_image" {
  type    = string
}

variable "cloud_run_name" {
  type    = string
  default = "gbms-priceprotection-service"
}

variable "dashboard_name" {
  type    = string
  default = "gbms-priceprotection-service"
}

variable "apigee_base_path" {
  type    = string
  default = "/pro/gb-backend-e2e"
}

variable "image_tag" {
  type    = string
  default = "latest"
}

variable "service_vpc_connector" {
  type        = string
  description = "The VPC Network Connector that this cloud run can connect to. It should be set up as fully-qualified URI. The format of this field is projects/*/locations/*/connectors/*. It should be 'projects/prj-pp-gen-preprod-net-acc7/locations/us-central1/connectors/preprod-gen-central1' for preprod project and 'projects/prj-p-gen-priv-net-19b4/locations/us-central1/connectors/prod-priv-gen-central1' for prod project."
  default     = "projects/prj-pp-gen-preprod-net-acc7/locations/us-central1/connectors/preprod-gen-central1"
}

variable "slo_name" {
  description = "Name of the SLO in Datadog"
  type = string
  default = "gbms-priceprotection-service"
}

variable "slo_description" {
  description = "Description of the SLO"
  type = string
  default = "SLO for gbms-priceprotection-service"
}

variable "primary_slo_timeframe" {
  description = "Primary time window for the SLO should be 7d, 30d or 90d"
  type = string
  default = "7d"
}

variable "monitor_name" {
  description = "Name of the Datadog monitor"
  type = string
  default = "gbms-priceprotection-service"
}
variable "critical_error_budget" {
  description = "The critical error budget for service in percentage. When the consumed error budget reaches this threshold, a critical alert will be triggered."
  type = number
  default = 70
}
variable "warning_error_budget" {
  description = "The warning error budget for service in percentage. When the consumed error budget reaches this threshold, a warning alert will be triggered."
  type = number
  default = 50
}

variable "critical_burn_rate" {
  description = " The critical burn rate for service in percentage. When the consumed burn rate reaches this threshold, a critical alert will be triggered."
  type = number
  default = 16.8
}

variable "warning_burn_rate" {
  description = "The warning burn rate for service in percentage. When the consumed burn rate reaches this threshold, a warning alert will be triggered."
  type = number
  default = 8.4
}


variable "burn_rate_long_window" {
  description = "Long window for burn rate alerts"
  type = string
  default = "1h"
}

variable "burn_rate_short_window" {
  description = "Short window for burn rate alerts"
  type = string
  default = "5m"
}


variable "priority" {
  description = "Priority of alarm, this attribute is an integer value that can range from 1 to 5, where 1 is the highest priority and 5 is the lowest priority "
  type        = number
  default     = 3
}

variable "notify_no_data" {
  description = "Whether the monitor should notify when there's no data"
  type        = bool
  default     = false
}

variable "renotify_interval" {
  description = "How much time that must pass before a notification is sent again for an alert that has already been triggered"
  type        = number
  default     = 0
}

variable "timeout_h" {
  description = "The maximum amount of time that an alert can be active before a notification is sent indicating that the alert has timed out"
  type        = number
  default     = 0
}

variable "target_service_level_objective" {
  description = "The target service level objective for service availability in percentage."
  type = string
  default = "99"
}

variable "warning_service_level_objective" {
  description = "The warning service level objective for service availability in percentage."
  type = string
  default = "99.5"
}

variable "monitor_alert_window" {
  description = "Time window for the monitor to create an alert."
  type = string
  default = "7d"
}

variable "product_name" {
  description = "Name of the product, the same name will be used as a team name. If a team with this name is not already present in datadog, contact Observability team to create it."
  type = string
  default = "Government Deals"
}

variable "product_group_name" {
  description = "Name of the Product group"
  type = string
  default = "Sales Solution"
}

variable "product_line_name" {
  description = "Name of the Product line"
  type = string
  default = "Incentives & Bids "
}

variable "eams_id" {
  description = "EAMS ID of the project"
  type = string
  default = "23367"
}

variable "slack_channel_name" {
  description = "Name of the slack channel to be notified"
  type        = string
  default     = "C0615AME92R"
}

variable "victorops_team_name" {
  description = "Name of the victorops team"
  type = string
  default = ""
}

variable "psa_project_id" {
  description = "PSA Project ID"
  type = string
}

variable "project_number" {
  description = "Common project number"
  type = string
}

variable "psa_project_number" {
  description = "Common project number"
  type = string
}

 