#!/bin/sh

sudo -u wrapper /home/swrapper/artifactRun.sh "$CI_PROJECT_NAME" "$CI_COMMIT_REF_NAME" "$CI_PROJECT_ID" "$CI_JOB_ID" "$CI_PROJECT_NAME-final-git-$CI_COMMIT_REF_NAME.jar" false
