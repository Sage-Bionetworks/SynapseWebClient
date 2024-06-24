# React in SWC via JsInterop

This is a brief guide to show how to use JsInterop to render a React component from the Synapse React Client in this GWT app.

## Pre-Tasks

This guide assumes:

- that your React component is complete
- your component is exported in the UMD bundle in SRC.
- the correct version of SRC has been specified in [package.json](../package.json)
- you've created GWT Widget with a View class that will handle your React Component

## Tasks

Using JsInterop to call your React component can be summarized in these basic steps:

1. Create a class to represent your components' props
1. Add your component to the SRC JsInterop type
1. Add a ReactComponent to your View
1. In the View, use React and ReactDOM to render the element.

### Create a Prop Class

Create a class in `org.sagebionetworks.web.client.jsinterop` that matches your prop type. You want to ensure that the class is annotated with `@JsType`, which will ensure that the class is compatible with the JavaScript runtime environment. Your class should also extend [ReactComponentProps](../src/main/java/org/sagebionetworks/web/client/jsinterop/ReactComponentProps.java), which just nominally indicates that this class is used to represent a JsInterop prop type.

As a convention, we implement a static `create` method on these types to make it easier to instantiate an object. These methods have the `@JsOverlay` annotation. `@JsOverlay` methods are only callable from Java, so you could do light validation here, but note some JsInterop constraints are still present.

For an example, see [EvaluationCardProps](../src/main/java/org/sagebionetworks/web/client/jsinterop/EvaluationCardProps.java)

### Expose your component in SRC via JsInterop

The [SRC class](../src/main/java/org/sagebionetworks/web/client/jsinterop/SRC.java) represents the Synapse React Client UMD bundle. Assuming your component is bundled in `SynapseComponents`, add your component as a static field.

Make sure you specify your prop type in the type parameter, and also make sure that your object name exactly matches the named export in `SynapseComponents`.

### Add a ReactComponent to your View

While you can append your React component to any element, we have [ReactComponent](../src/main/java/org/sagebionetworks/web/client/widget/ReactComponent.java) that contains logic that simplifies managing the lifecycle of a React component. Add this to your View in code or `*.ui.xml` file, and make sure you can reference it for the next step.

### Passing Synapse context

If your application uses Synapse context (e.g. uses authentication to call the Synapse API), then you will also need to pass a context provider. To do so, you can inject [SynapseReactClientFullContextPropsProvider](../src/main/java/org/sagebionetworks/web/client/context/SynapseReactClientFullContextPropsProvider.java), which will create the wrapping context for you.

### Render the element

How you manage updating your widget's view will vary based on the scenario, but when you're ready to render the component, this is all you have to do:

```java
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;

class MyView {

  // Typically injected
  SynapseReactClientFullContextPropsProvider propsProvider;

  void renderComponent() {
    MyProps props = props.create(/**/);
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.MyComponent,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(reactNode);
  }
}

```

The React component will render when `MyView.renderComponent` is invoked.

## Notes and Pain Points

- You should try to use JsInterop whenever possible, but in some cases you may have to use JSNI. See Resources to get an idea of what some of the constraints may be
- Java 8+ lambdas cannot be used in JsInterop code. See [issue](https://github.com/gwtproject/gwt/issues/9354).

## Resources

- [JsInterop // GWT Docs](http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJsInterop.html)
- [JsInterop Cookbook](https://github.com/google/j2cl/blob/master/docs/jsinterop-by-example.md)
