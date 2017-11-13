import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval
import jenkins.CLI

def setup(config){
    def env = System.getenv()
    def instance = Jenkins.getInstance()
    def executersCount = env['JENKINS_ENV_EXECUTERS']
    def cliOverRemoting = env['JENKINS_ENV_CLI_REMOTING_ENABLED']
    def useScriptSecurity = env['JENKINS_ENV_USE_SCRIPT_SECURITY']
    def changeWorkspaceDir = env['JENKINS_ENV_CHANGE_WORKSPACE_DIR']
    
    def jenkinsUrl = env['JENKINS_ENV_JENKINS_URL']
    def adminAddress = env['JENKINS_ENV_ADMIN_ADDRESS']

    if(jenkinsUrl || adminAddress){
        def jenkinsLocationConfig = jenkins.model.JenkinsLocationConfiguration.get()
        if(jenkinsUrl){
            jenkinsLocationConfig.url  = jenkinsUrl
        }
        if(adminAddress){
            jenkinsLocationConfig.adminAddress = adminAddress
        }
        jenkinsLocationConfig.save()
    }
    

    instance.setNumExecutors(executersCount  ? executersCount.toInteger() : 0)
    CLI.get().setEnabled(cliOverRemoting ? cliOverRemoting.toBoolean() : false)
    // This is the only way to change the workspaceDir field at the moment... ):
    // We do that if the JENKINS_HOME is mapped to NFS volume (e.g. deployment on ECS or Kubernetes)
    if(changeWorkspaceDir){
        def f = Jenkins.getDeclaredField('workspaceDir')
        f.setAccessible(true)
        f.set(Jenkins.instance, '/jenkins-workspace-home/workspace/${ITEM_FULLNAME}')
        Jenkins.instance.save()
    }


    jenkins.model.GlobalConfiguration.all()
        .get(javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration).useScriptSecurity =
            useScriptSecurity ? useScriptSecurity.toBoolean() : false
}
return this