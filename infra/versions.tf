terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.50"
    }
  }

  # Empty backend — bucket/key/region injected via -backend-config at init
  backend "s3" {}
}

provider "aws" {
  region = var.region
}
