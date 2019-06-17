#
# Variables Configuration
#

variable "cluster-name" {
  default = "terraform-eks-hello"
  type    = "string"
}


variable "access_key" {
	description = "AWS access key"
	default = ""
}

variable "secret_key" {
	description = "AWS secret key"
	default = ""
}

variable "region" {
	description = "AWS region for hosting our your network"
	default = ""
}

variable "key_name" {
	description = "Key name for SSHing into EC2"
	default = "hello"
}

variable "ami-jenkins" {
	description = "Base AMI to launch the instances"
	default = {
	us-east-1 		= "ami-03e8bf6cdeb27ed10"
	us-east-2 		= "ami-0908fc0b26eefb4ca"
	us-west-2 		= "ami-0e32ec5bc225539f5"
	}
}
