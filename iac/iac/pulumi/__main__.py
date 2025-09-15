import pulumi
import pulumi_aws as aws
import json

# VPC
vpc = aws.ec2.Vpc("my-vpc",
    cidr_block="10.0.0.0/16"
)

# Subnets
subnet_a = aws.ec2.Subnet("public-subnet-a",
    vpc_id=vpc.id,
    cidr_block="10.0.1.0/24",
    map_public_ip_on_launch=True,
    availability_zone="us-east-1a"
)

subnet_b = aws.ec2.Subnet("public-subnet-b",
    vpc_id=vpc.id,
    cidr_block="10.0.2.0/24",
    map_public_ip_on_launch=True,
    availability_zone="us-east-1b"
)

# Internet Gateway
igw = aws.ec2.InternetGateway("igw", vpc_id=vpc.id)

# Route Table
route_table = aws.ec2.RouteTable("public-route-table",
    vpc_id=vpc.id,
    routes=[{
        "cidr_block": "0.0.0.0/0",
        "gateway_id": igw.id,
    }]
)

# Associations
rta_a = aws.ec2.RouteTableAssociation("rta-a",
    subnet_id=subnet_a.id,
    route_table_id=route_table.id
)

rta_b = aws.ec2.RouteTableAssociation("rta-b",
    subnet_id=subnet_b.id,
    route_table_id=route_table.id
)

# Security Groups
alb_sg = aws.ec2.SecurityGroup("alb-sg",
    vpc_id=vpc.id,
    description="Allow HTTP inbound traffic",
    ingress=[{
        "protocol": "tcp",
        "from_port": 80,
        "to_port": 80,
        "cidr_blocks": ["0.0.0.0/0"],
    }],
    egress=[{
        "protocol": "-1",
        "from_port": 0,
        "to_port": 0,
        "cidr_blocks": ["0.0.0.0/0"],
    }]
)

ecs_sg = aws.ec2.SecurityGroup("ecs-sg",
    vpc_id=vpc.id,
    description="Allow traffic from ALB",
    ingress=[{
        "protocol": "tcp",
        "from_port": 8080,
        "to_port": 8080,
        "security_groups": [alb_sg.id],
    }],
    egress=[{
        "protocol": "-1",
        "from_port": 0,
        "to_port": 0,
        "cidr_blocks": ["0.0.0.0/0"],
    }]
)

# ECS Cluster
ecs_cluster = aws.ecs.Cluster("ecs-cluster")

# CloudWatch Log Group
log_group = aws.cloudwatch.LogGroup("ecs-log-group",
    name="/ecs/api-crud",
    retention_in_days=7
)

# Task Definition (con fix de .apply)
task_def = aws.ecs.TaskDefinition("task-def",
    family="api-crud-task",
    cpu="256",
    memory="512",
    network_mode="awsvpc",
    requires_compatibilities=["FARGATE"],
    execution_role_arn="arn:aws:iam::210364787680:role/LabRole",
    container_definitions=log_group.name.apply(
        lambda lg_name: json.dumps([{
            "name": "api-crud",
            "image": "gonzalovalladolid/api-students:latest",  # tu imagen en Docker Hub
            "portMappings": [{
                "containerPort": 8080,
                "hostPort": 8080,
                "protocol": "tcp"
            }],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": lg_name,
                    "awslogs-region": "us-east-1",
                    "awslogs-stream-prefix": "ecs"
                }
            }
        }])
    )
)

# Load Balancer
alb = aws.lb.LoadBalancer("alb",
    internal=False,
    security_groups=[alb_sg.id],
    subnets=[subnet_a.id, subnet_b.id]
)

target_group = aws.lb.TargetGroup("target-group",
    port=8080,
    protocol="HTTP",
    target_type="ip",
    vpc_id=vpc.id,
    health_check={
        "path": "/students",
        "matcher": "200-399"
    }
)

listener = aws.lb.Listener("listener",
    load_balancer_arn=alb.arn,
    port=80,
    default_actions=[{
        "type": "forward",
        "target_group_arn": target_group.arn
    }]
)

# ECS Service
service = aws.ecs.Service("ecs-service",
    cluster=ecs_cluster.arn,
    desired_count=1,
    launch_type="FARGATE",
    task_definition=task_def.arn,
    network_configuration={
        "assignPublicIp": True,
        "subnets": [subnet_a.id, subnet_b.id],
        "security_groups": [ecs_sg.id],
    },
    load_balancers=[{
        "target_group_arn": target_group.arn,
        "container_name": "api-crud",
        "container_port": 8080,
    }],
    opts=pulumi.ResourceOptions(depends_on=[listener])
)

# Outputs
pulumi.export("alb_dns", alb.dns_name)
pulumi.export("vpc_id", vpc.id)
