def slurper = new ConfigSlurper()
// fix classloader problem using ConfigSlurper in job dsl
slurper.classLoader = this.class.classLoader
def config = slurper.parse(readFileFromWorkspace('devops/jenkins-generator/variables.dsl'))

config.app_job.each { name, data ->
  println "generating deploy $name"
  //println name
  //println data
  createJob(name, data)
}

config.list_views.each { name, data ->
  println "generating view $name"
  //println name
  //println data
  createView(name, data)
}

config.app_job.each { name, data ->
  println "generating Pipeline $name"
  //println name
  //println data
  createPipeline(name, data)
}


def createJob(app, data){

	job("deploy-${app}") {
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

	    steps {
			shell(getShell(app, data))
	    }

        try {
		     publishers {
        		buildPipelineTrigger("rollback-${app}")
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
	println "${data}"

    String var_shell
    var_shell="""
cd devops/kubernetes
kubectl set image deployment/${app} ${app}=${data.user_dockerhub}/${app}:\$GIT_COMMIT"""

 	return var_shell
}


def createView(app, data) {

	listView("${app}") {
	    description("${data.description}")
	    filterBuildQueue()
	    filterExecutors()
	    jobs {
	        //name('release-projectA')
	        regex(/${data.regex}/)
	    }
	    columns {
	        status()
	        weather()
	        name()
	        lastSuccess()
	        lastFailure()
	        lastDuration()
	        buildButton()
	    }
	}

}


def createPipeline(app, data) {
	deliveryPipelineView("Pipeline ${app}") {
	    pipelineInstances(5)
	    showAggregatedPipeline()
	    columns(3)
	    sorting(Sorting.TITLE)
	    updateInterval(10)
	    enableManualTriggers()
	    showAvatars()
	    showChangeLog()
	    allowPipelineStart()
	    pipelines {
	        component("build-${app}", "build-${app}")
	    }
	}
}