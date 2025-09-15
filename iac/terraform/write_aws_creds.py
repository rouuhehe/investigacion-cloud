#export AWS_ACCESS_KEY_ID="...del lab..."           #Variables de entorno con credenciales AWS
#export AWS_SECRET_ACCESS_KEY="...del lab..."       #Variables de entorno con credenciales AWS
#export AWS_SESSION_TOKEN="...del lab..."           #Variables de entorno con credenciales AWS

import os,configparser,pathlib
p=pathlib.Path("/home/ubuntu/.aws/credentials"); p.parent.mkdir(parents=True,exist_ok=True)
c=configparser.RawConfigParser(); c.add_section("default")
c.set("default","aws_access_key_id",os.environ["AWS_ACCESS_KEY_ID"])
c.set("default","aws_secret_access_key",os.environ["AWS_SECRET_ACCESS_KEY"])
c.set("default","aws_session_token",os.environ["AWS_SESSION_TOKEN"])
with open(p,"w") as f: c.write(f)
print("Escrito",p)



#aws sts get-caller-identity --region us-east-1
