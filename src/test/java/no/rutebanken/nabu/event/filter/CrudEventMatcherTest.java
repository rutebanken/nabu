/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.rutebanken.nabu.event.filter;

import no.rutebanken.nabu.domain.event.CrudEvent;
import no.rutebanken.nabu.event.user.AdministrativeZoneRepository;
import no.rutebanken.nabu.event.user.dto.TypeDTO;
import no.rutebanken.nabu.event.user.dto.responsibility.EntityClassificationDTO;
import no.rutebanken.nabu.event.user.dto.user.EventFilterDTO;
import no.rutebanken.nabu.event.user.model.AdministrativeZone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CrudEventMatcherTest {

    private static final String STOP_ENTITY_TYPE = "StopPlace";

    private static final String BUS_ENTITY_CLASSIFICATION = "onstreetBus";

    private AdministrativeZoneRepository administrativeZoneRepositoryMock;

    @BeforeEach
    void setUp() {
        administrativeZoneRepositoryMock = mock(AdministrativeZoneRepository.class);
    }

    @Test
    void eventMatchingFilterWithoutAdminZonesSpecificType() {
        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier(BUS_ENTITY_CLASSIFICATION).build();
        Assertions.assertTrue(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(BUS_ENTITY_CLASSIFICATION)).matches(event));
    }

    @Test
    void eventMatchingFilterWithoutAdminZonesWildcardType() {
        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier("whatever").build();
        Assertions.assertTrue(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(EventMatcher.ALL_TYPES)).matches(event));
    }


    @Test
    void eventInAdminZoneMatching() {
        AdministrativeZone zone = adminZone();
        EventFilterDTO filterWithAdminZone = filter(BUS_ENTITY_CLASSIFICATION);
        filterWithAdminZone.getAdministrativeZoneRefs().add(zone.getId());

        when(administrativeZoneRepositoryMock.getAdministrativeZone(zone.getId())).thenReturn(zone);

        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier(BUS_ENTITY_CLASSIFICATION).geometry(zone.getPolygon().getCentroid()).build();
        Assertions.assertTrue(new CrudEventMatcher(administrativeZoneRepositoryMock, filterWithAdminZone).matches(event));
    }

    @Test
    void eventOutsideAdminZoneNotMatching() {
        AdministrativeZone zone = adminZone();
        EventFilterDTO filterWithAdminZone = filter(BUS_ENTITY_CLASSIFICATION);
        filterWithAdminZone.getAdministrativeZoneRefs().add(zone.getId());

        when(administrativeZoneRepositoryMock.getAdministrativeZone(zone.getId())).thenReturn(zone);

        Point pointOutside = new GeometryFactory().createPoint(new Coordinate(-50, -50));

        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier(BUS_ENTITY_CLASSIFICATION).geometry(pointOutside).build();
        Assertions.assertFalse(new CrudEventMatcher(administrativeZoneRepositoryMock, filterWithAdminZone).matches(event));
    }

    @Test
    void eventWrongTypeNotMatchingFilter() {
        CrudEvent event = CrudEvent.builder().entityType("NotMatchingType").entityClassifier(BUS_ENTITY_CLASSIFICATION).build();
        Assertions.assertFalse(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(EventMatcher.ALL_TYPES)).matches(event));
    }

    @Test
    void eventWrongClassificationNotMatchingFilter() {
        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier("onstreetTram").build();
        Assertions.assertFalse(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(BUS_ENTITY_CLASSIFICATION)).matches(event));
    }

    private EventFilterDTO filter(String stopPlaceTypeClassificationCode) {
        EntityClassificationDTO entityTypeStopPlaceClassification = new EntityClassificationDTO();
        entityTypeStopPlaceClassification.privateCode = STOP_ENTITY_TYPE;
        entityTypeStopPlaceClassification.entityType = new TypeDTO();
        entityTypeStopPlaceClassification.entityType.privateCode = "EntityType";


        EntityClassificationDTO stopPlaceTypeClassification = new EntityClassificationDTO();
        stopPlaceTypeClassification.privateCode = stopPlaceTypeClassificationCode;
        stopPlaceTypeClassification.entityType = new TypeDTO();
        stopPlaceTypeClassification.entityType.privateCode = "StopPlaceType";

        EventFilterDTO eventFilter = new EventFilterDTO(EventFilterDTO.EventFilterType.CRUD);
        eventFilter.entityClassifications.add(entityTypeStopPlaceClassification);
        eventFilter.entityClassifications.add(stopPlaceTypeClassification);
        return eventFilter;
    }


    private AdministrativeZone adminZone() {

        GeometryFactory fact = new GeometryFactory();
        LinearRing linear = new GeometryFactory().createLinearRing(new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 0)});
        return new AdministrativeZone("test", "name", new Polygon(linear, null, fact));
    }
}
