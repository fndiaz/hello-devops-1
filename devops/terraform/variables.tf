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
	us-west-1 		= "ami-09cb0bdb44f1c6711"
	us-west-2 		= "ami-0e32ec5bc225539f5"
	us-east-1 		= "ami-011a42c1bf0740e35"
	us-east-2 		= "ami-0908fc0b26eefb4ca"
	sa-east-1 		= "ami-0318cb6e2f90d688b"
	ca-central-1 	= "ami-0f2cb2729acf8f494"
	ap-south-1		= "ami-04ea996e7a3e7ad6b"
	ap-northeast-1  = "ami-06c43a7df16e8213c"
	ap-northeast-2	= "ami-0e0f4ff1154834540"
	ap-southeast-1  = "ami-0eb1f21bbd66347fe"
	eu-west-1  		= "ami-0773391ae604c49a4"
	eu-west-2		= "ami-061a2d878e5754b62"
	eu-west-3		= "ami-075b44448d2276521"
	}
}
