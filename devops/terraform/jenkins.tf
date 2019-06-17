resource "aws_key_pair" "hello" {
  key_name   = "hello"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC7YWTOajE9lQzfDxekJdGVFMhUX9YALeVDHi10bMSP+mpS4ZE3OXOMFxKLv1IkO+PyHyZUEbBzbrhVYneudhfH25E+2astv8aPNnyouVzRSMJc3F7f6z0V2jXjpv3Kn740bvO5neL7L2kk2+CrJzufEZP0g3MX8pBsN2DCdF2+Ay0zOC5cp6ruEPDR0Gh/FbctKg2ktSfIJDBenmwtRnTiE2St0MvZ1KGSoblaWVRCwVGgSItcpBJIialzFJCR5/eyhRnf0LVf1/jj0RztZIW2YC+GyVzQytkZggXHHCwS/CG7av3fmH+CgdEuvDTOwE347WpjiXDHRqKOQ4qubnDCR5f42dI419V1p9wdy1JKdbgU36JCZnbxtAP1kIWH14BSW+jfe828jLGwLuLBaQMy5eunbSr3fs/rk3fWx14F4ztXmlIEndTskK/rsjwfnW0QiCG4Sv3V/zlZRza4R/vfiIQlhAu3CYW6uccHgBahSgpyryvODifZHQHAL5QX8UPew6RaJ32EFUBJYHooJlW9TaXjgA2L1gyIJfIXSBOqEoY+5+xh/UmDERjBi9FdYJRYe3jfjjkVyWtrE3wFNApsbZmcOi6IzUA/w0nA7EA0Mc/K3C/jMw1WMpiJWxKHL6L0eDXNBDoJbgnM53jyProZM+3U9veOoNRd+dVZRXay7w=="
}


resource "aws_instance" "jenkins" {
	depends_on   = ["aws_eks_cluster.hello"]
	ami = "${lookup(var.ami-jenkins, var.region)}"
	instance_type = "t2.micro"
	key_name = "${var.key_name}"
	subnet_id = "${aws_subnet.hello.0.id}"
	vpc_security_group_ids = ["${aws_security_group.jenkins.id}"]
	associate_public_ip_address = true


	tags {
	Name = "jenkins"
	}

	provisioner "file" {
          content = "${data.template_file.kubeconfig.rendered}"
          destination = "/tmp/config"
	}

       provisioner "file" {
         content = "${data.template_file.configmap.rendered}"
         destination = "/tmp/configmap.yaml"
       }

       provisioner "file" {
         content = "${data.template_file.aws.rendered}"
         destination = "/tmp/aws"
       }

    connection {
      type        = "ssh"
      private_key = "${file("/root/key-jenkins-hello/id_rsa")}"
      user        = "ec2-user"
      timeout     = "2m"
    }
}


data "template_file" "kubeconfig" {
  template = "${file("templates/kubeconfig")}"
vars = {
    cluster = "${aws_eks_cluster.hello.endpoint}"
    certificate = "${aws_eks_cluster.hello.certificate_authority.0.data}"
    clustername = "${var.cluster-name}"
  }
}

data "template_file" "configmap" {
  template = "${file("templates/configmap")}"
vars = {
   rolearn = "${aws_iam_role.hello-node.arn}"
  }
}

data "template_file" "aws" {
  template = "${file("templates/aws")}"
vars = {
   accesskey = "${var.access_key}"
   secretkey = "${var.secret_key}"
  }
}

resource "aws_security_group" "jenkins" {
  name        = "jekins"
  description = "rules jenkins"
  vpc_id      = "${aws_vpc.hello.id}"

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    cidr_blocks     = ["0.0.0.0/0"]
  }

  tags {
    Name = "jenkins"
  }
}
