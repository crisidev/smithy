package software.amazon.smithy.aws.traits.endpointdiscovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.utils.SetUtils;

public class EndpointDiscoveryIndexTest {

    @Test
    public void getsDiscoveryInformation() {
        Model result = Model.assembler()
                .discoverModels(getClass().getClassLoader())
                .addImport(getClass().getResource("test-model.json"))
                .assemble()
                .unwrap();
        EndpointDiscoveryIndex discoveryIndex = result.getKnowledge(EndpointDiscoveryIndex.class);

        ShapeId service = ShapeId.from("ns.foo#FooService");
        ShapeId operation = ShapeId.from("ns.foo#GetObject");
        EndpointDiscoveryInfo info = discoveryIndex.getEndpointDiscoveryInfo(service, operation).get();

        assertTrue(info.isRequired());
        assertEquals(service, info.getService().getId());
        assertEquals(operation, info.getOperation().getId());
        assertEquals(ShapeId.from("ns.foo#DescribeEndpoints"), info.getDiscoveryOperation().getId());
        assertEquals(ShapeId.from("ns.foo#InvalidEndpointError"), info.getError().getId());
        assertEquals(1, info.getDiscoveryIds().size());
        assertEquals(ShapeId.from("ns.foo#GetObjectInput$Id"), info.getDiscoveryIds().get(0).getId());
    }

    @Test
    public void handlesOperationsWithoutConfiguration() {
        Model result = Model.assembler()
                .discoverModels(getClass().getClassLoader())
                .addImport(getClass().getResource("test-model.json"))
                .assemble()
                .unwrap();
        EndpointDiscoveryIndex discoveryIndex = result.getKnowledge(EndpointDiscoveryIndex.class);

        ShapeId service = ShapeId.from("ns.foo#FooService");
        ShapeId operation = ShapeId.from("ns.foo#DescribeEndpoints");
        Optional<EndpointDiscoveryInfo> info = discoveryIndex.getEndpointDiscoveryInfo(service, operation);

        assertFalse(info.isPresent());
    }

    @Test
    public void handlesServicesWithoutConfiguration() {
        Model result = Model.assembler()
                .discoverModels(getClass().getClassLoader())
                .addImport(getClass().getResource("test-model.json"))
                .assemble()
                .unwrap();
        EndpointDiscoveryIndex discoveryIndex = result.getKnowledge(EndpointDiscoveryIndex.class);

        ShapeId service = ShapeId.from("ns.foo#BarService");
        ShapeId operation = ShapeId.from("ns.foo#GetObject");
        Optional<EndpointDiscoveryInfo> info = discoveryIndex.getEndpointDiscoveryInfo(service, operation);

        assertFalse(info.isPresent());
    }

    @Test
    public void getsDiscoveryOperations() {
        Model result = Model.assembler()
                .discoverModels(getClass().getClassLoader())
                .addImport(getClass().getResource("test-model.json"))
                .assemble()
                .unwrap();
        EndpointDiscoveryIndex discoveryIndex = result.getKnowledge(EndpointDiscoveryIndex.class);
        ShapeId service = ShapeId.from("ns.foo#FooService");
        Set<ShapeId> discoveryOperations = discoveryIndex.getEndpointDiscoveryOperations(service);

        ShapeId getObject = ShapeId.from("ns.foo#GetObject");
        ShapeId putObject = ShapeId.from("ns.foo#PutObject");
        Set<ShapeId> expected = SetUtils.of(getObject, putObject);

        assertEquals(expected, discoveryOperations);
    }
}
