package no.rutebanken.nabu.rest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import no.rutebanken.nabu.domain.SystemJobStatus;
import no.rutebanken.nabu.domain.event.JobState;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonRootName("systemStatusAggregation")
public class SystemStatusAggregation {

    @JsonProperty("jobDomain")
    public String jobDomain;

    @JsonProperty("jobType")
    public String jobType;

    @JsonProperty("currentState")
    public JobState currentState;

    @JsonProperty("currentStateDate")
    public Date currentStateDate;

    @JsonProperty("latestDatePerState")
    public Map<JobState, Date> latestDatePerState = new HashMap<>();


    public SystemStatusAggregation(SystemJobStatus in) {
        this.jobType = in.getAction();
        this.jobDomain = in.getJobDomain();
    }


    public void addSystemStatus(SystemJobStatus in) {

        Date statusDate=Date.from(in.getLastStatusTime());
        if (currentStateDate == null || currentStateDate.before(statusDate)) {
            currentStateDate = statusDate;
            currentState = in.getState();
        }

        Date latestDateForState = latestDatePerState.get(in.getState());
        if (latestDateForState == null || latestDateForState.before(statusDate)) {
            latestDatePerState.put(in.getState(), statusDate);
        }

    }
}
