#!/bin/bash -e

# clone the repository and the release-branch

if [ "$target" == "productized" ]; then
   git clone git@github.com:jboss-integration/dashbuilder.git --branch $releaseBranch
else 
   git clone git@github.com:dashbuilder/dashbuilder.git --branch $releaseBranch
fi

commitMsg="Tagging $tag"
cd $WORKSPACE/dashbuilder
# pushes the TAG to github.com:jboss-integration or github.com:dashbuilder [IMPORTANT: "push -n" (--dryrun) should be replaced by "push" when script is ready]
if [ "$target" == "productized" ]; then
   git tag -a $TAG -m "$commitMsg"
   git remote add upstream git@github.com:jboss-integration/dashbuilder.git
   git push upstream $tag
else
   git tag -a $TAG -m "$commitMsg"
   git push origin $tag
fi
