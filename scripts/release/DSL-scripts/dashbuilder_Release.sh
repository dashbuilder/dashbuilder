def dashDeploy=
"""
sh /home/jenkins/workspace/DASHB-Release-0.6.x/release-scripts/dashbuilder/scripts/release/DSL-scripts/DASHB_Deploy.sh
"""

def dashPushTags=
"""
sh /home/jenkins/workspace/DASHB-Release-0.6.x/release-scripts/dashbuilder/scripts/release/DSL-scripts/DASHB_PushTags.sh
"""

def dashUpdateVersions=
"""
sh /home/jenkins/workspace/DASHB-Release-0.6.x/release-scripts/dashbuilder/scripts/release/DSL-scripts/DASHB_UpdateVersions.sh
"""

// ******************************************************

job("01.DASHB_Release-0.6.x") {

  description("This job: <br> releases dashbuilder, upgrades the version, builds and deploys, copies artifacts to Nexus, closes the release on Nexus  <br> <b>IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.<b>")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    choiceParam("BASE_BRANCH", ["master", "7.0.x"], "please select the base branch <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "r0.6.0.Beta6", "please edit the name of the release branch <br> i.e. typically <b> r0.6.0.Beta6 </b> for <b> community </b>or <b> bsync-7.0.x-2017.01.20  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("newVersion", "0.6.0.Beta6", "please edit the old version used in the poms<br> The version old should typically look like <b> 0.6.0.Beta6 </b> for <b> community </b> or <b> 0.6.0.20170120-productized </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("UBERFIRE_VERSION", "1.0.0.Beta6", "please edit the version of uberfire <br> The version should typically look like <b> 1.0.0.Beta6 </b> for <b> community </b> or <b> 1.0.0.20170120-productized </b> for <b> productization </b> <br> ******************************************************** <br> ")
    choiceParam("ERRAI_VERSION", ["4.0.0.Beta6", "4.0.0.Beta7"], "please select the needed errai version <br> ******************************************************** <br> ")
  }
  
  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  customWorkspace("/home/jenkins/workspace/DASHB-Release-0.6.x")

  wrappers {
    timeout {
      absolute(60)
    }
    timestamps()
    colorizeOutput()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(dashDeploy)
  }
}

// ******************************************************

job("02.DASHB_PushTags-0.6.x") {

  description("This job: <br> creates and pushes the tags for <br> community (dashbuilder) or product (jboss-integration) <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community: <b> community </b> or <br> if it is for building a productization tag: <b>productized <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r0.5.0.Final </b> for <b> community </b>or <b> bsync-6.5.x-2016.08.05  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("TAG_NAME", "tag", "The tag should typically look like <b> 0.5.0.Final </b> for <b> community </b> or <b> sync-6.5.0.2016.08.05 </b> for <b> productization </b> <br> ******************************************************** <br> ")
  };

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  wrappers {
    timeout {
      absolute(30)
    }
    timestamps()
    colorizeOutput()
    preBuildCleanup()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(dashPushTags)
  }
}

// ******************************************************

job("03.DASHB_updateVersion-0.6.x") {

  description("This job: <br> updates dashbuilder repository to a new developmenmt version <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
 
  parameters {
    stringParam("newVersion", "new dashbuilder version", "Edit the new dashbuilder version")
    stringParam("BASE_BRANCH", "base branch", "please select the base branch <br> ******************************************************** <br> ")
  }

  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  wrappers {
    timeout {
      absolute(30)
    }
    timestamps()
    colorizeOutput()
    preBuildCleanup()
    toolenv("APACHE_MAVEN_3_2_5", "JDK1_8")
  }

  configure { project ->
    project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
      cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
        killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
        killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
        username 'jenkins'
      }
    }
  }

  publishers {
    mailer('mbiarnes@redhat.com', false, false)
  }

  steps {
    environmentVariables {
        envs(MAVEN_OPTS :"-Xms2g -Xmx3g", MAVEN_HOME: "\$APACHE_MAVEN_3_2_5_HOME", MAVEN_REPO_LOCAL: "/home/jenkins/.m2/repository", PATH :"\$MAVEN_HOME/bin:\$PATH")
    }
    shell(dashUpdateVersions)
  }
}

// *************************
// *************************

nestedView("DASHB_Releases-0.6.x") {
    views {
        listView("dashbuilder-0.6.x") {
            jobs {
                name("01.DASHB_Release-0.6.x")
                name("02.DASHB_PushTags-0.6.x")
                name("03.DASHB_updateVersion-0.6.x")
            }
            columns {
                status()
                weather()
                name()
                lastSuccess()
                lastFailure()
            }
        }
    }
}
