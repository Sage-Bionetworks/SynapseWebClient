## Integration tests for entity CRUD
## 
## Author: Nicole Deflaux <nicole.deflaux@sagebase.org>
#################################################################################

integrationTestCRUD <- 
  function() 
{
  ## Create a project
  project <- RJSONIO::emptyNamedList
  project$name = paste('R Synapse CRUD Integration Test Project', gsub(':', '_', date()))
  project <- Project(project)
  createdProject <- createEntity(entity=project)
  checkEquals(propertyValue(project, "name"), propertyValue(createdProject, "name"))
  
  ## Create a dataset
  dataset <- RJSONIO::emptyNamedList
  dataset$name = 'R Integration Test Dataset'
  dataset$status = 'test create'
  dataset$parentId <- propertyValue(createdProject, "id")
  createdDataset <- synapseClient:::synapsePost(uri='/dataset', entity=dataset)
  checkEquals(dataset$name, createdDataset$name)
  checkEquals(dataset$status, createdDataset$status)
  
  ## Get a dataset
  storedDataset <- synapseClient:::synapseGet(uri=createdDataset$uri)
  checkEquals(dataset$name, storedDataset$name)
  checkEquals(dataset$status, storedDataset$status)
  
  ## Modify a dataset
  storedDataset$status <- 'test update'
  modifiedDataset <- synapseClient:::synapsePut(uri=storedDataset$uri, entity=storedDataset)
  checkEquals(dataset$name, modifiedDataset$name)
  checkTrue(dataset$status != modifiedDataset$status)
  checkEquals('test update', modifiedDataset$status)
  
  ## Get dataset annotations
  annotations <- synapseClient:::getAnnotations(entity=modifiedDataset)
  annotations$stringAnnotations$myNewAnnotationKey <- 'my new annotation value'
  storedAnnotations <- synapseClient:::updateAnnotations(annotations=annotations)
  checkEquals('my new annotation value', storedAnnotations$stringAnnotations$myNewAnnotationKey)
  
  ## Delete a dataset
  synapseClient:::synapseDelete(uri=modifiedDataset$uri)
  
  ## Confirm that its gone
  checkException(synapseClient:::synapseGet(uri=createdDataset$uri))
  
  ## Delete a Project
  deleteEntity(entity=createdProject)
}
