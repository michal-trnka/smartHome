echo "deploying"
eval `ssh-agent -s`
pwd
echo "ls /temp"
ls /temp/
echo "adding key"
ssh-add /tmp/deploy_rsa
echo "ssh added"
ssh pi@129.62.149.69 pwd