package com.capgroup.digital.pss.fcr.services.infra;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.capgroup.digital.pss.fcr.services.infra.CdkContext.ServiceDefinition;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.Tags;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterAttributes;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.ecs.IFargateService;
import software.amazon.awscdk.services.ecs.LogDrivers;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.iam.Policy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;

public class PssFCRServicesInfraStack extends Stack {
	
	private static final Logger LOGGER = Logger.getLogger(PssFCRServicesInfraStack.class.getName());

	public PssFCRServicesInfraStack(final Construct parent, final String id) {
		this(parent, id, null,null);
	}

	public PssFCRServicesInfraStack(final Construct parent, final String id, final StackProps props,CdkContext context) {
		super(parent, id, props);

		String vpcId=context.getVpcId();
		
		IVpc vpc = Vpc.fromLookup(this, vpcId, new VpcLookupOptions.Builder().vpcId(vpcId).build());
		
		List<ISubnet> subnets= context.getSubnetGroups()
									.stream()
									.map(  subnetGroup -> Subnet.fromSubnetId(this, subnetGroup.getSubnetId(), subnetGroup.getSubnetId()))
									.collect(Collectors.toList());
		
		String serviceName = context.getService().getName();
		SubnetSelection subnetSelection = new SubnetSelection.Builder()
				 	                            .subnets(subnets)
												.build();
		
		ServicePrincipal ecsTasksServicePrinicipal = ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build();
		
		Role iamRole = software.amazon.awscdk.services.iam.Role.Builder.create(this, "IAM-Role")
							.assumedBy(ecsTasksServicePrinicipal)
							.description("ECS Task User - " + serviceName + " - " + context.getEnvironment().getRegion())
							.roleName(serviceName + "Role")
							.build();
		
		
		Map<String,String> tags = context.getTags();
		Tags.of(iamRole).add("usage-id", tags.get("usageId"));
		Tags.of(iamRole).add("sd-period",  tags.get("sdPeriod"));
		Tags.of(iamRole).add("exp-date", tags.get("expDate"));
		Tags.of(iamRole).add("ppmc-id" , tags.get("ppmcId"));
		Tags.of(iamRole).add("toc",  tags.get("toc"));
		Tags.of(iamRole).add("env-type",  tags.get("env-type"));
		Tags.of(iamRole).add("cost-center",  tags.get("costCenter"));
		
		
		PolicyStatement ecrPolicy = software.amazon.awscdk.services.iam.PolicyStatement.Builder.create().actions(Collections.singletonList("ecr:*")).resources(Collections.singletonList("*")).build();
		PolicyStatement LogStreamPolicy = software.amazon.awscdk.services.iam.PolicyStatement.Builder.create().actions(Arrays.asList("logs:CreateLogStream", "logs:PutLogEvents")).resources(Collections.singletonList("*")).build();
		PolicyStatement ssmPolicy = PolicyStatement.Builder.create().actions(Arrays.asList("ssm:Describe*", "ssm:List*", "ssm:Get*", "ssm:Put*")).resources(Collections.singletonList("*")).build();
		PolicyStatement KmsPolicy = PolicyStatement.Builder.create().actions(Arrays.asList("Kms:List*", "Kms:Describe*", "Kms:Get*", "Kms:Decrypt*", "Kms:Encrypt*", "Kms:GenerateDataKey*", "Kms:RecryptTo*", "Kms:DecribeKey*", "Kms:RecryptFrom*")).resources(Collections.singletonList("*")).build();
//		PolicyStatement sqsPolicy;
//		if (serviceTaskConfiguration.getSqsPolicyResourcePattern() ! = null) {
//		    sqsPolicy = software.amazon.awscdk.servies.iam.PolicyStatement.Builder.create().actions(Arrays.asList(Array.asList("sqs:*")).resources(Collections.singletonList(serviceTaskConfiguration.getSqsPolicyResourcePattern())).build();
//		} else {
//		    sqsPolicy = null;
//		}
//
//
//		PolicyStatement s3Policy;
//		if (serviceTaskConfiguration.getS3PolicyResourcePattern() != null) {
//		    s3Policy = software.amazon.awscdk.servies.iam.PolicyStatement.Builder.create().actions(Arrays.asList("s3:*")).resources(serviceTaskConfiguration.getS3PolicyResourcePattern()).build();
//		} else {
//		    s3Policy = null;
//		    
//		}
		
		List<PolicyStatement> statements = Arrays.asList(ecrPolicy,LogStreamPolicy,ssmPolicy,KmsPolicy).stream().filter(Objects::nonNull).collect(Collectors.toList());
		Policy policy =software.amazon.awscdk.services.iam.Policy.Builder.create(this, "Task Role Policy")
		.policyName("TaskRolePolicy"+ context.getService().getName() + "-" + context.getEnvironment().getRegion())
		.roles(Collections.singletonList(iamRole)).statements(statements).build();

		Tags.of(iamRole).add("usage-id", tags.get("usageId"));
		Tags.of(iamRole).add("sd-period",  tags.get("sdPeriod"));
		Tags.of(iamRole).add("exp-date", tags.get("expDate"));
		Tags.of(iamRole).add("ppmc-id" , tags.get("ppmcId"));
		Tags.of(iamRole).add("toc",  tags.get("toc"));
		Tags.of(iamRole).add("env-type",  tags.get("env-type"));
		Tags.of(iamRole).add("cost-center",  tags.get("costCenter"));
		iamRole.addToPolicy(ecrPolicy);
		iamRole.addToPolicy(LogStreamPolicy);
		iamRole.addToPolicy(ssmPolicy);
		iamRole.addToPolicy(KmsPolicy);
		LogGroup serviceLogGroup = software.amazon.awscdk.services.logs.LogGroup.Builder.create(this, "ServiceLogGroup").logGroupName(serviceName).removalPolicy(RemovalPolicy.DESTROY).build();
	
		AwsLogDriverProps serviceLogDriverProps = AwsLogDriverProps.builder().logGroup(serviceLogGroup).streamPrefix("/aws/ecs/" ).build();
		software.amazon.awscdk.services.ecs.@NotNull LogDriver serviceLogDriver = LogDrivers.awsLogs(serviceLogDriverProps);
		String clusterName = context.getClusterName();
		List<ISecurityGroup> securityGroups = context.getSecurityGroups()
				.stream()
				.map( sg -> SecurityGroup.fromLookup(this, sg, sg))
				.collect(Collectors.toList());
		ICluster serviceCluster;
		if (clusterName == null || "".equalsIgnoreCase(clusterName)) {
			serviceCluster = Cluster.Builder.create(this, "ECSCluster")
								.clusterName("pss")
                    			.vpc(vpc).build();
			LOGGER.info("creating new ECS cluster");
			
		} else {
		    ClusterAttributes clusterAttributes = ClusterAttributes.builder()
		    										.clusterName(clusterName)
		    										.vpc(vpc)
		    										.securityGroups(securityGroups)
		    										.build();
		    serviceCluster = Cluster.fromClusterAttributes(this, "ECSCluster", clusterAttributes);
		    LOGGER.info("Importing the existing ECS cluster");
		}
		createOrUpdateFargateService(serviceCluster,vpc,securityGroups,subnetSelection,context,iamRole);
		


	}
	
	
	public void createOrUpdateFargateService(ICluster cluster, IVpc vpc, 
			List<ISecurityGroup> securityGroups,
			SubnetSelection subnetSelection,
			CdkContext context, Role taskRole) {
		// Use the ECS Application Load Balanced Fargate Service construct to create a ECS
		ServiceDefinition service = context.getService();	
		
		ApplicationLoadBalancedFargateService fargateService =  ApplicationLoadBalancedFargateService
																.Builder
																.create(this, "ApplicationLoadBalancedFargateService")
																.cluster(cluster)
																.cpu(service.getCpu())
																.assignPublicIp(true)
																.desiredCount(service.getCount())
																.memoryLimitMiB(service.getMemory())
																.serviceName(service.getName())
																.enableEcsManagedTags(true)

																.taskImageOptions(
																	new ApplicationLoadBalancedTaskImageOptions.Builder()
																		.containerName(service.getName())
																		.containerPort(8080)
																		.taskRole(taskRole)
																		.executionRole(taskRole)
																		.family(service.getName())																		

																		.image(ContainerImage.fromRegistry(service.getServiceContainerImage()))
																		
																		.build()																		
																
																)

																.publicLoadBalancer(true) 
																.securityGroups(securityGroups)
																.targetProtocol(ApplicationProtocol.HTTPS)
																.listenerPort(443)
																
																.taskSubnets(subnetSelection)
																.build();
		
	    fargateService.getTargetGroup().configureHealthCheck(new HealthCheck.Builder().healthyHttpCodes("200")
                .path(service.getTargetGroupHealth())
				.port("8080")
				.build());

	}
	
	
	

	
	
	

}
