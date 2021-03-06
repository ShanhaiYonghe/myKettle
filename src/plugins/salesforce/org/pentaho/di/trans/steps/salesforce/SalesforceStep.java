/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.salesforce;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public abstract class SalesforceStep extends BaseStep implements StepInterface {

  public static Class<?> PKG = SalesforceStep.class;

  public SalesforceStepMeta meta;
  public SalesforceStepData data;

  public SalesforceStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( !super.init( smi, sdi ) ) {
      return false;
    }
    meta = (SalesforceStepMeta) smi;
    data = (SalesforceStepData) sdi;

    String realUrl = environmentSubstitute( meta.getTargetURL() );
    String realUsername = environmentSubstitute( meta.getUsername() );
    String realPassword = environmentSubstitute( meta.getPassword() );
    String realModule = environmentSubstitute( meta.getModule() );

    if ( Utils.isEmpty( realUrl ) ) {
      log.logError( BaseMessages.getString( PKG, "SalesforceStep.TargetURLMissing.Error" ) );
      return false;
    }
    if ( Utils.isEmpty( realUsername ) ) {
      log.logError( BaseMessages.getString( PKG, "SalesforceInput.UsernameMissing.Error" ) );
      return false;
    }
    if ( Utils.isEmpty( realPassword ) ) {
      log.logError( BaseMessages.getString( PKG, "SalesforceInput.PasswordMissing.Error" ) );
      return false;
    }
    if ( Utils.isEmpty( realModule ) ) {
      log.logError( BaseMessages.getString( PKG, "SalesforceInputDialog.ModuleMissing.DialogMessage" ) );
      return false;
    }
    try {
      // The final step should call data.connection.connect(), as other settings may set additional options
      data.connection = new SalesforceConnection( log, realUrl, realUsername, realPassword );
      data.connection.setModule( realModule );
      data.connection.setTimeOut( Const.toInt( environmentSubstitute( meta.getTimeout() ), 0 ) );
      data.connection.setUsingCompression( meta.isCompression() );
    } catch ( KettleException ke ) {
      logError( BaseMessages.getString( PKG, "SalesforceInput.Log.ErrorOccurredDuringStepInitialize" )
        + ke.getMessage() );
      return false;
    }
    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.connection != null ) {
      try {
        data.connection.close();
      } catch ( KettleException ignored ) {
        /* Ignore */
      }
      data.connection = null;
    }
    super.dispose( smi, sdi );
  }
}
