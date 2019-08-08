//import groovy.transform.TypeChecked

withConfig(configuration) {
  configuration.setDisabledGlobalASTTransformations(['groovy.grape.GrabAnnotationTransformation'] as Set)
  //ast(TypeChecked)
}