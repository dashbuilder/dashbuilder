def dashDeploy=
"""
sh /home/jenkins/workspace/DASHBUILDER-Release-0.6.x/release-scripts/dashbuilder/scripts/release/DSL-scripts/DASHB_deploy.sh
"""

def dashPushTag=
"""
sh /home/jenkins/workspace/DASHBUILDER-Release-0.6.x/release-scripts/dashbuilder/scripts/release/DSL-scripts/DASHB_pushTag.sh
"""

def dashUpdateVersion=
"""
sh /home/jenkins/workspace/DASHBUILDER-Release-0.6.x/release-scripts/dashbuilder/scripts/release/DSL-scripts/DASHB_updateVersion.sh
"""

// ******************************************************

job("DASHB_release-0.6.x") {

  description("This job: <br> releases dashbuilder, upgrades the version, builds and deploys, copies artifacts to Nexus, closes the release on Nexus  <br> <b>IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.<b>")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community <b> community </b> or <br> if it is for building a productization tag <b>productized <br> ******************************************************** <br> ")
    stringParam("BASE_BRANCH", "base branch", "please select the base branch <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r0.6.0.Beta6 </b> for <b> community </b>or <b> bsync-6.5.x-2017.01.20  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("newVersion", "new version", "please edit the old version used in the poms<br> The version old should typically look like <b> 0.6.0.Beta6 </b> for <b> community </b> or <b> 6.5.1.20170120-productized </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("UBERFIRE_VERSION", "uberfire version", "please edit the version of uberfire <br> The version should typically look like <b> 1.0.0.Beta6 </b> for <b> community </b> or <b> 6.5.1.20170120-productized </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("ERRAI_VERSION", "errai version", "please select the needed errai version <br> ******************************************************** <br> ")
  }
  
  label("kie-releases")

  logRotator {
    numToKeep(10)
  }

  jdk("jdk1.8")

  wrappers {
    timeout {
      absolute(60)
    }
    timestamps()
    preBuildCleanup()
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

job("DASHB_pushTag-0.6.x") {

  description("This job: <br> creates and pushes the tags for <br> community (dashbuilder) or product (jboss-integration) <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

  parameters {
    choiceParam("TARGET", ["community", "productized"], "please select if this release is for community: <b> community </b> or <br> if it is for building a productization tag: <b>productized <br> ******************************************************** <br> ")
    stringParam("RELEASE_BRANCH", "release branch", "please edit the name of the release branch <br> i.e. typically <b> r0.6.0.Final </b> for <b> community </b>or <b> bsync-6.5.x-2016.08.05  </b> for <b> productization </b> <br> ******************************************************** <br> ")
    stringParam("TAG", "tag", "The tag should typically look like <b> 0.6.0.Final </b> for <b> community </b> or <b> sync-6.5.x-2016.08.05 </b> for <b> productization </b> <br> ******************************************************** <br> ")
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
    shell(dashPushTag)
  }
}

// ******************************************************

job("DASHB_updateVersion-0.6.x") {

  description("This job: <br> updates dashbuilder repository to a new developmenmt version <br> IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")
 
  parameters {
    stringParam("newVersion", "new dashbuilder version", "Edit the new dashbuilder version")
    stringParam("BASE_BRANCH", "base branch", "please select the base branch <br> ******************************************************** <br> ")
    stringParam("UF_DEVEL_VERSION", "uberfire development version", "Edit the uberfire development version")
    stringParam("ERRAI_DEVEL_VERSION", "errai development version", "Edit the errai development version")
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
    shell(dashUpdateVersion)
  }
}

// *************************
// *************************

listView("0.6.x-dashbuilder-releases") {
    description("all scripts needed to build dashbuilder release")
    jobs {
                name("DASHB_release-0.6.x")
                name("DASHB_pushTag-0.6.x")
                name("DASHB_updateVersion-0.6.x")
    }
    columns {
                status()
                weather()
                name()
                lastSuccess()
                lastFailure()
    }
}
