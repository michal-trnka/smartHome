echo "deploying"
eval `ssh-agent -s`
pwd
echo "next"
ls
echo "next"
ls ../
echo "next"
ls ../../
echo "next"
ssh-add key
echo "ssh added"
ssh pi@129.62.149.69 pwd