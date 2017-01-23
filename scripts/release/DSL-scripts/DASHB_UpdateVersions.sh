TARGET_USER=kiereleaseuser
REMOTE_URL=git@github.com:kiereleaseuser/dashbuilder.git
DATE=$(date "+%Y-%m-%d")

# clone the repository and branch of dashbuilder
git clone git@github.com:dashbuilder/dashbuilder.git --branch $BASE_BRANCH
cd $WORKSPACE/dashbuilder
PR_BRANCH=dashbuilder-$DATE-$BASE_BRANCH
git checkout -b $PR_BRANCH $BASE_BRANCH
git remote add $TARGET_USER $REMOTE_URL

# upgrades the version to next development version of Uberfire
sh scripts/release/update-version.sh $newVersion

# git add and commit the version update changes 
git add .
commitMSG="update to next development version $newVersion"
git commit -m "$commitMSG"

# do a build of dashbuilder
mvn -B -e -U clean install -Dfull -Dgwt.memory.settings="-Xmx3g -Xms1g -Xss1M"

# Raise a PR
SOURCE=dashbuilder
git push $TARGET_USER $PR_BRANCH
hub pull-request -m "$commitMSG" -b $SOURCE:$BASE_BRANCH -h $TARGET_USER:$PR_BRANCH
