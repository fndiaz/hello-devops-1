def slurper = new ConfigSlurper()
// fix classloader problem using ConfigSlurper in job dsl
slurper.classLoader = this.class.classLoader
def config = slurper.parse(readFileFromWorkspace('devops/jenkins-generator/variables.dsl'))

config.app_job.each { name, data ->
  println "generating build $name"
  //println name
  //println data
  createJob(name, data)
}



def createJob(app, data){

	job("build-${app}") {
		scm {
	    	git {
				remote {
	            	url(data.url)
	            	credentials(data.credentials_git)
	            }
	        	branch(data.branch)
	    	}
		}

		logRotator {
			daysToKeep(-1)
			numToKeep(data.rotate_builds)
			artifactDaysToKeep(-1)
			artifactNumToKeep(-1)
		}

		wrappers {
            credentialsBinding {
            usernamePassword('DOCKERUSER', 'DOCKERPASS', 'credentials_docker')
        }
    	}

	    steps {
			shell(getShell(app, data))
	    }

        try {
		    publishers {
				downstream("deploy-${app}", "SUCCESS")
			}
        }
        catch (MissingPropertyExceptionmpe) {
        	println 'pos build nao configurado'
        }


        try {
        	triggers {
            	cron(data."${enviroment}".cron)
            }
        }
        catch (MissingPropertyExceptionmpe) {
        	println 'cron nao configurado'
        }

	}
}


private String getShell(app, data) {

    String var_shell
    var_shell="""
cd ${app}
docker build -t ${data.user_dockerhub}/${app}:\$GIT_COMMIT .
echo \$DOCKERUSER
echo \$DOCKERPASS
docker login -u \$DOCKERUSER -p \$DOCKERPASS
docker push ${data.user_dockerhub}/${app}:\$GIT_COMMIT"""

 	return var_shell
}






