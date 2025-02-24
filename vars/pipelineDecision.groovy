#!groovy

// here nodejsVM, javaVM and nodejsEKS are the different environments, in catalogue jenkins file we pass the values there which environment we want and here it selects and executes, means this shared library is acting as to support in different environments and not to write the pipeines from the scratch. 

def decidePipeline(Map configMap){
    application = configMap.get("application")
    switch(application) {
        case 'nodejsVM':
            nodejsVM(configMap)    
            break
        case 'javaVM':
            javaVM(configMap)
            break
        case 'nodejsEKS':
            nodejsEKS(configMap)
            break
        default: 
            error "no application recognized"
            break
    }
}