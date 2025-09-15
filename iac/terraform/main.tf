terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

variable "region" { default = "us-east-1" }
variable "repo"   { default = "crud" }

provider "aws" { region = var.region }

data "aws_caller_identity" "me" {}
data "aws_vpc" "default" { default = true }

data "aws_subnets" "subs" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

resource "aws_security_group" "alb" {
  name        = "crud-alb-sg"
  description = "ALB HTTP"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "svc" {
  name        = "crud-svc-sg"
  description = "Allow 8000 from ALB"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 8000
    to_port         = 8000
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_lb" "alb" {
  name               = "crud-alb"
  load_balancer_type = "application"
  internal           = false
  security_groups    = [aws_security_group.alb.id]
  subnets            = data.aws_subnets.subs.ids
}

resource "aws_lb_target_group" "tg" {
  name        = "crud-tg"
  port        = 8000
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = data.aws_vpc.default.id

  health_check {
    path = "/students"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tg.arn
  }
}

resource "aws_ecs_cluster" "cluster" { name = "crud-cluster" }

resource "aws_cloudwatch_log_group" "lg" {
  name              = "/ecs/crud"
  retention_in_days = 7
}

data "aws_iam_role" "labrole" { name = "LabRole" }

locals {
  image_uri = "${data.aws_caller_identity.me.account_id}.dkr.ecr.${var.region}.amazonaws.com/${var.repo}:latest"
}

resource "aws_ecs_task_definition" "td" {
  family                   = "crud"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = data.aws_iam_role.labrole.arn
  task_role_arn            = data.aws_iam_role.labrole.arn

  container_definitions = jsonencode([{
    name  = "crud",
    image = local.image_uri,
    portMappings = [{ containerPort = 8000 }],
    logConfiguration = {
      logDriver = "awslogs",
      options = {
        awslogs-group         = aws_cloudwatch_log_group.lg.name,
        awslogs-region        = var.region,
        awslogs-stream-prefix = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "svc" {
  name            = "crud-svc"
  cluster         = aws_ecs_cluster.cluster.id
  task_definition = aws_ecs_task_definition.td.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    assign_public_ip = true
    subnets          = data.aws_subnets.subs.ids
    security_groups  = [aws_security_group.svc.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.tg.arn
    container_name   = "crud"
    container_port   = 8000
  }

  depends_on = [aws_lb_listener.http]
}

output "alb_dns" { value = aws_lb.alb.dns_name }
