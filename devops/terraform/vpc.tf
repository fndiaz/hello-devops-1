#
# VPC Resources
#  * VPC
#  * Subnets
#  * Internet Gateway
#  * Route Table
#

resource "aws_vpc" "hello" {
  cidr_block = "10.0.0.0/16"

  tags = "${
    map(
      "Name", "terraform-eks-hello-node",
      "kubernetes.io/cluster/${var.cluster-name}", "shared",
    )
  }"
}

resource "aws_subnet" "hello" {
  count = 2

  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  cidr_block        = "10.0.${count.index}.0/24"
  vpc_id            = "${aws_vpc.hello.id}"

  tags = "${
    map(
      "Name", "terraform-eks-hello-node",
      "kubernetes.io/cluster/${var.cluster-name}", "shared",
    )
  }"
}

resource "aws_internet_gateway" "hello" {
  vpc_id = "${aws_vpc.hello.id}"

  tags = {
    Name = "terraform-eks-hello"
  }
}

resource "aws_route_table" "hello" {
  vpc_id = "${aws_vpc.hello.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.hello.id}"
  }
}

resource "aws_route_table_association" "hello" {
  count = 2

  subnet_id      = "${aws_subnet.hello.*.id[count.index]}"
  route_table_id = "${aws_route_table.hello.id}"
}
