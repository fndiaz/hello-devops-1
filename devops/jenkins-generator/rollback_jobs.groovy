def slurper = new ConfigSlurper()
// fix classloader problem using ConfigSlurper in job dsl
slurper.classLoader = this.class.classLoader
def config = slurper.parse(readFileFromWorkspace('devops/jenkins-generator/variables.dsl'))

config.app_job.each { name, data ->
  println "generating rollback $name"
  //println name
  //println data
  createJob(name, data)
}



def createJob(app, data){

	job("rollback-${app}") {
		scm {
	    	git {
				remote {
	            	url(data.url)
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

	    steps {
			shell(getShell(app, data))
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
	println "${data}"

    String var_shell
    var_shell="""
cd devops/kubernetes
kubectl rollout undo deployment/${app}"""

 	return var_shell
}




