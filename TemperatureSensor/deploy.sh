echo "starting ssh agent"
eval `ssh-agent -s`
echo "changing key rights"
chmod 600 /tmp/deploy_rsa
echo "adding ssh key"
ssh-add /tmp/deploy_rsa
echo "ssh added"
echo "connecting to remote server"
ssh pi@129.62.149.69 pwd