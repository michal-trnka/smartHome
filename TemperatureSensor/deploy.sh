echo "deploying"
eval `ssh-agent -s`
pwd
ls
ls ../
ls ../../
ssh-add key
echo "ssh added"
ssh pi@129.62.149.69 pwd