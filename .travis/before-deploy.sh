#!/bin/bash -x

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then    
    
	ls -ltr .travis/*
  
	openssl aes-256-cbc -K $encrypted_6415f14baeb5_key -iv $encrypted_6415f14baeb5_iv -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d      
  
	ls -ltr .travis/*
    	
    	# --yes for disabling the prompt
	gpg -v --yes --batch --fast-import .travis/codesigning.asc
fi