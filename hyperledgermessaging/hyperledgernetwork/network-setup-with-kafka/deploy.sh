 git archive --remote=ssh://git@forum-subver-01/home/git/forumcrypto.git HEAD |  tar xvf - hyperledgernetwork/network-setup-with-kafka/*
 
 mv scripts/script.sh .
 
 chmod 755 script.sh
 
 ./script.sh