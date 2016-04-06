#!/usr/bin/env bash -x

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then    
    
    ls -ltr .travis/*
    
    openssl aes-256-cbc -K $encrypted_ca544fcb7638_key -iv $encrypted_ca544fcb7638_iv -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d
        
    ls -ltr .travis/*
    
    gpg -v --fast-import .travis/codesigning.asc
fi
