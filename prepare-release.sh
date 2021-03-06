#!/usr/bin/env bash

function updateReleaseVersion() {
    RELEASE_TYPE=$1
    if [[ ${RELEASE_TYPE} == "MAJOR" ]]
    then
        ./mvnw build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.0.0 -DgenerateBackupPoms=false
    elif [[ ${RELEASE_TYPE} == "PATCH" ]]
    then
        ./mvnw build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} -DgenerateBackupPoms=false
    else
        ./mvnw build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.0 -DgenerateBackupPoms=false
    fi
}

function getBranchVersion() {
    VERSION=`./mvnw -q build-helper:parse-version -Dexec.executable="echo" -Dexec.args='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | tail -n 1`
    echo ${VERSION}
}

function getVersion() {
    VERSION=`./mvnw -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | tail -n 1`
    echo ${VERSION}
}

function updateSnapshotVersion() {
    RELEASE_TYPE=$1
    updateReleaseVersion ${RELEASE_TYPE}
    if [[ ${RELEASE_TYPE} == "PATCH" ]]
    then
        echo "Patch not allowed at this branch"
        exit 1
    else
        ./mvnw build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}.0-SNAPSHOT -DgenerateBackupPoms=false
    fi
}

set +e
git diff --exit-code >/dev/null 2>&1
GIT_STATUS=$?
set -e

if [[ ! ${GIT_STATUS} -eq 0 ]]
then
    echo "Aborting due to uncommited changes"
    exit 1
fi

CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`
TYPE=$1


if [[ ${TYPE} == "PATCH" ]]
then
if [[ ${CURRENT_BRANCH} == "master" ]]
    then
        echo "Patch not allowed at this branch"
        exit 1
    fi
fi

TMPDIR=`mktemp -d`
echo "Copying to ${TMPDIR}"

cp -pr . ${TMPDIR}
echo "Done"

cd ${TMPDIR}


updateReleaseVersion ${TYPE}

RELEASE_VERSION=$(getVersion)
echo Build release ${RELEASE_VERSION} without running tests

./mvnw clean install -DskipTests

if [[ ${TYPE} != "PATCH" ]]
then
    BRANCH_NAME=v$(getBranchVersion)
    echo Branch ${BRANCH_NAME}
    git checkout -b ${BRANCH_NAME}
    git commit -am"TASK Prepare release ${RELEASE_VERSION}"

    git checkout ${CURRENT_BRANCH}

    updateSnapshotVersion ${TYPE}

    DEVELOPMENT_VERSION=$(getVersion)
    echo Preparing next development version ${DEVELOPMENT_VERSION}

    git commit -am"Set next development version ${DEVELOPMENT_VERSION}"
elif [[ ${TYPE} == "PATCH" ]]
then
    if [[ ${CURRENT_BRANCH} != "master" ]]
    then
        git commit -am"TASK Prepare release ${RELEASE_VERSION}"
    fi
fi

echo Build directory ${TMPDIR}
